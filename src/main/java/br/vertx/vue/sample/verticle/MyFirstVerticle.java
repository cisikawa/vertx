package br.vertx.vue.sample.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class MyFirstVerticle extends AbstractVerticle {

	@Override
	public void start(Future<Void> fut) throws Exception {
		vertx.createHttpServer().requestHandler(r -> {
			if (r.path().equals("/")) {
				r.response().sendFile("html/index.html");
			}
		}).listen(8080, result -> {
			if (result.succeeded()) {
				fut.complete();
			} else {
				fut.fail(result.cause());
			}
		});
	}

}
