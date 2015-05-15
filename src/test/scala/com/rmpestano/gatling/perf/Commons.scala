/**
 * Created by pestano on 15/05/15.
 */
package com.rmpestano.gatling.perf
import io.gatling.core.session.Expression
import _root_.io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Commons {

  val httpProtocol = http
    .baseURL("http://www.primefaces.org/showcase")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .inferHtmlResources()
    .connection( """keep-alive""")
    .contentTypeHeader("*/*")
    .acceptLanguageHeader("pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3")
    .userAgentHeader("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:36.0) Gecko/20100101 Firefox/36.0")

  val jsfViewStateCheck = css("input[name='javax.faces.ViewState']", "value")
    .saveAs("viewState")

  val jsfPartialViewStateCheck = xpath("//update[contains(@id,'ViewState')]")
    .saveAs("viewState")


  val formIdCheck = css("form", "id")
    .saveAs("form")



  def jsfGet(name: String, url: Expression[String]) =
    http(name)
      .get(url)
      .check(jsfViewStateCheck)

  def jsfPost(name: String, url: Expression[String]) = http(name)
    .post(url)
    .formParam("javax.faces.ViewState", "${viewState}")
    .check(jsfViewStateCheck)

  def jsfPartialPost(name: String, url: Expression[String]) = http(name)
    .post(url)
    .header("Faces-Request", "partial/ajax")
    .formParam("javax.faces.partial.ajax", "true")
    .formParam("javax.faces.ViewState", "${viewState}")
    .check(jsfPartialViewStateCheck)
}