package com.wanbo.easyapi.ui

import org.eclipse.jetty.rewrite.handler.RewritePatternRule

/**
 * Rewrite handler
 * Created by wanbo on 15/8/26.
 */
class RewriteHandler(basePath: String, replacePath: String) extends org.eclipse.jetty.rewrite.handler.RewriteHandler {

    this.setRewriteRequestURI(true)
    this.setRewritePathInfo(false)

    val rewriteRule = new RewritePatternRule
    rewriteRule.setPattern(basePath)
    rewriteRule.setReplacement(replacePath)

    this.addRule(rewriteRule)
}
