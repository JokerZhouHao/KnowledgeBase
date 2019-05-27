package main.sp;

import entity.sp.QueryParams;

public interface SPInterface extends Runnable{
	public abstract void test(QueryParams qp) throws Exception;
}
