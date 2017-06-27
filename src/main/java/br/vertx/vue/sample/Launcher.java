package br.vertx.vue.sample;

import java.io.IOException;

import br.vertx.vue.sample.verticle.MyFirstRXVerticle;
import br.vertx.vue.sample.verticle.MyFirstVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class Launcher {

	public static void main(final String[] args) throws IOException {
		
		DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", 8080)
				.put("url", "jdbc:hsqldb:mem:test?shutdown=true").put("driver_class", "org.hsqldb.jdbcDriver"));
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(MyFirstVerticle.class.getName(), options);
	}

}
