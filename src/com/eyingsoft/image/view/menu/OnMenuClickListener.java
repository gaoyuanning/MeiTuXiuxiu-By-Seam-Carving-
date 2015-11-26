package com.eyingsoft.image.view.menu;

import android.view.View;
import android.widget.AdapterView;

/**
 * 菜单项目选中监听器
 */
public interface OnMenuClickListener
{
	/**
	 * 菜单项被点击的会调用的方法
	 */
	public void onMenuItemClick(AdapterView<?> parent, View view, int position);
	
	/**
	 * 菜单隐藏会调用的方法
	 */
	public void hideMenu();
}
