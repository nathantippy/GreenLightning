package com.javanut.gl.benchmark;


import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public class PoolManager {

	private final transient PgConnectOptions options;
	private transient PoolOptions poolOptions;
	private PgPool pool;
	
	public PoolManager(PgConnectOptions options, PoolOptions poolOptions) {
		this.options = options;
		this.poolOptions = poolOptions;
	}
		
	public PgPool pool() {
		if (null==pool) {			
			pool = PgPool.pool(options, poolOptions);			
		}
		return pool;
	}
	
	
}
