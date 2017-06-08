package br.vertx.vue.sample;

import io.vertx.core.Vertx;

public class Launcher {

	public static void main(final String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle("br.vertx.vue.sample.verticle.MyFirstVerticle");
	}

}
