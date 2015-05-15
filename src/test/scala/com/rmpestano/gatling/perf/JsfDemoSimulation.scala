package com.rmpestano.gatling.perf


import _root_.io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.core.session.Expression

class JsfDemoSimulation extends Simulation {


  val httpProtocol = http
    .baseURL("http://www.primefaces.org/showcase")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .inferHtmlResources()
    .connection( """keep-alive""")
    .contentTypeHeader("*/*")
    .acceptLanguageHeader("pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3")
    .userAgentHeader("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:36.0) Gecko/20100101 Firefox/36.0")


  //JSF requests and scenarios

  val jsfViewStateCheck = css("input[name='javax.faces.ViewState']", "value")
    .saveAs("viewState")

  val jsfPartialViewStateCheck = xpath("//update[contains(@id,'ViewState')]")
    .saveAs("viewState")

  val ajaxButtonIdCheck = css("button[id$='ajax']", "id")
    .saveAs("btAjax")

  val nonAjaxButtonIdCheck = css("button[id$='nonAjax']", "id")
    .saveAs("btNonAjax")

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

  def ajaxButtonCall = jsfPartialPost("request_ajax_button", "/ui/button/commandButton.xhtml")
    .formParam("javax.faces.source", "${btAjax}")
    .formParam("javax.faces.partial.execute", "@all")
    .formParam("${btAjax}", "${btAjax}")
    .formParam("${form}", "${form}")
    .check(status.is(200))

  def normalButtonRequest =
    jsfPost("normal_request", "/ui/button/commandButton.xhtml")
      .formParam("${btNonAjax}", "${btNonAjax}")
      .formParam("${form}", "${form}")
      .check(status.is(200))


  val jsfScenario = scenario("jsf")
    .exec(
      jsfGet("saveState", "/ui/button/commandButton.xhtml")
        .check(status.is(200), ajaxButtonIdCheck, formIdCheck, nonAjaxButtonIdCheck)
    )
    .pause(2)

    //just prints the session
    .exec(session => {
    println(session)
    session
  })
    .pause(100 milliseconds)
    .exec(normalButtonRequest)
    .pause(1)

    .exec(session => {
    println(session)
    session
  })

  setUp(
    jsfScenario.inject(atOnceUsers(5))
  )
    .protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.greaterThan(95)
    )


}