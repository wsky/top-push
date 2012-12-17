package com.tmall.top.push;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class InitServlet extends HttpServlet {
	@Override
	public void init(ServletConfig config) throws ServletException {
		ClientManager.Init(config);
		//worker
		
		super.init(config);
	}
}
