package com.wanbo.easyapi.ui.pages

import com.wanbo.easyapi.ui.RedirectHandler

/**
 * Home page
 * Created by wanbo on 15/8/27.
 */
class HomePage extends WebPage {

    this.attachHandler(new RedirectHandler("/sss/", "/servers"))

}