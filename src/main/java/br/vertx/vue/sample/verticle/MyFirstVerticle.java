package br.vertx.vue.sample.verticle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.vertx.vue.sample.model.Whisky;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class MyFirstVerticle extends AbstractVerticle {

	private JDBCClient jdbc;

	private Map<Integer, Whisky> products = new LinkedHashMap<>();

	@Override
	public void start(Future<Void> fut) throws Exception {
		jdbc = JDBCClient.createShared(vertx, config(), "My-Whisky-Collection");

		startBackend((connection) -> createSomeData(connection,
				(nothing) -> startWebApp((http) -> completeStartup(http, fut)), fut), fut);

		createSomeData();

	}

	private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
		if (http.succeeded()) {
			fut.complete();
		} else {
			fut.fail(http.cause());
		}
	}

	private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
		// Create a router object.
		Router router = Router.router(vertx);

		// Bind "/" to our hello message.
		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/html").end("<h1>Hello from my first Vert.x 3 application</h1>");
		});

		router.route("/assets/*").handler(StaticHandler.create("assets"));

		router.get("/api/whiskies").handler(this::getAll);
		router.route("/api/whiskies*").handler(BodyHandler.create());
		router.post("/api/whiskies").handler(this::addOne);
		router.get("/api/whiskies/:id").handler(this::getOne);
		router.put("/api/whiskies/:id").handler(this::updateOne);
		router.delete("/api/whiskies/:id").handler(this::deleteOne);

		// Create the HTTP server and pass the "accept" method to the request
		// handler.
		vertx.createHttpServer().requestHandler(router::accept).listen(
				// Retrieve the port from the configuration,
				// default to 8080.
				config().getInteger("http.port", config().getInteger("http.port")), next::handle);
	}

	private void startBackend(Handler<AsyncResult<SQLConnection>> next, Future<Void> fut) {
		jdbc.getConnection(ar -> {
			if (ar.failed()) {
				fut.fail(ar.cause());
			} else {
				next.handle(Future.succeededFuture(ar.result()));
			}
		});
	}

	private void createSomeData(AsyncResult<SQLConnection> result, Handler<AsyncResult<Void>> next, Future<Void> fut) {
		if (result.failed()) {
			fut.fail(result.cause());
		} else {
			SQLConnection connection = result.result();
			connection.execute("CREATE TABLE IF NOT EXISTS Whisky (id INTEGER IDENTITY, name varchar(100), "
					+ "origin varchar(100))", ar -> {
						if (ar.failed()) {
							fut.fail(ar.cause());
							connection.close();
							return;
						}
						connection.query("SELECT * FROM Whisky", select -> {
							if (select.failed()) {
								fut.fail(ar.cause());
								connection.close();
								return;
							}
							if (select.result().getNumRows() == 0) {
								insert(new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay"), connection,
										(v) -> insert(new Whisky("Talisker 57° North", "Scotland, Island"), connection,
												(r) -> {
													next.handle(Future.<Void>succeededFuture());
													connection.close();
												}));
							} else {
								next.handle(Future.<Void>succeededFuture());
								connection.close();
							}
						});
					});
		}
	}

	private void insert(Whisky whisky, SQLConnection connection, Handler<AsyncResult<Whisky>> next) {
		String sql = "INSERT INTO Whisky (name, origin) VALUES ?, ?";
		connection.updateWithParams(sql, new JsonArray().add(whisky.getName()).add(whisky.getOrigin()), (ar) -> {
			if (ar.failed()) {
				next.handle(Future.failedFuture(ar.cause()));
				return;
			}
			UpdateResult result = ar.result();
			// Build a new whisky instance with the generated id.
			Whisky w = new Whisky(result.getKeys().getInteger(0), whisky.getName(), whisky.getOrigin());
			next.handle(Future.succeededFuture(w));
		});
	}

	private void getOne(RoutingContext routingContext) {
		final String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			final Integer idAsInteger = Integer.valueOf(id);
			Whisky whisky = products.get(idAsInteger);
			if (whisky == null) {
				routingContext.response().setStatusCode(404).end();
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(whisky));
			}
		}
	}

	private void updateOne(RoutingContext routingContext) {
		final String id = routingContext.request().getParam("id");
		JsonObject json = routingContext.getBodyAsJson();
		if (id == null || json == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			final Integer idAsInteger = Integer.valueOf(id);
			Whisky whisky = products.get(idAsInteger);
			if (whisky == null) {
				routingContext.response().setStatusCode(404).end();
			} else {
				whisky.setName(json.getString("name"));
				whisky.setOrigin(json.getString("origin"));
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(whisky));
			}
		}
	}

	private void deleteOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Integer idAsInteger = Integer.valueOf(id);
			products.remove(idAsInteger);
		}
		routingContext.response().setStatusCode(204).end();
	}

	private void createSomeData() {
		Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
		products.put(bowmore.getId(), bowmore);
		Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
		products.put(talisker.getId(), talisker);
	}

	private void addOne(RoutingContext routingContext) {
		// Read the request's content and create an instance of Whisky.
		final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(), Whisky.class);
		// Add it to the backend map
		products.put(whisky.getId(), whisky);

		// Return the created whisky as JSON
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(whisky));
	}

	private void getAll(RoutingContext routingContext) {
		jdbc.getConnection(ar -> {
			SQLConnection connection = ar.result();
			connection.query("SELECT * FROM Whisky", result -> {
				List<Whisky> whiskies = result.result().getRows().stream().map(Whisky::new)
						.collect(Collectors.toList());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(whiskies));
				connection.close(); // Close the connection
			});
		});
	}

}
