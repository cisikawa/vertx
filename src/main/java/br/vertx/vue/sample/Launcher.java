package br.vertx.vue.sample;

import java.io.IOException;

import br.vertx.vue.sample.verticle.MyFirstVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class Launcher {

	public static void main(final String[] args) throws IOException {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(MyFirstVerticle.class.getName());
	}

}
