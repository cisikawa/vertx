package br.vertx.vue.sample.model;

import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.json.JsonObject;

public class Whisky {

	private static final AtomicInteger COUNTER = new AtomicInteger();

	private final int id;

	private String name;

	private String origin;

	public Whisky(String name, String origin) {
		this.id = COUNTER.getAndIncrement();
		this.name = name;
		this.origin = origin;
	}

	public Whisky() {
		this.id = COUNTER.getAndIncrement();
	}

	public Whisky(Integer id, String name, String origin) {
		this.id = id;
		this.name = name;
		this.origin = origin;
	}

	public Whisky(JsonObject json) {
		this.name = json.getString("NAME");
		this.origin = json.getString("ORIGIN");
		this.id = json.getInteger("ID");
	}

	public String getName() {
		return name;
	}

	public String getOrigin() {
		return origin;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}
}
