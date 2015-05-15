package com.rmpestano.gatling.perf


import _root_.io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.core.session.Expression
import com.rmpestano.gatling.perf.Commons._

class CommandButtonSimulation extends Simulation {


  //JSF requests and scenarios

  val ajaxButtonIdCheck = css("button[id$='ajax']", "id")
    .saveAs("btAjax") //stores in gatling session id of button with id that endswith 'ajax'

  val nonAjaxButtonIdCheck = css("button[id$='nonAjax']", "id")
    .saveAs("btNonAjax")


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

  def ajaxEventRequest = jsfPartialPost("request_ajax_event", "/ui/ajax/event.xhtml")
    .formParam("javax.faces.source", "${btAjax}")
    .formParam("javax.faces.partial.execute", "@all")
    .formParam("${btAjax}", "${btAjax}")
    .formParam("${form}", "${form}")
    .check(status.is(200))

  val commandButtonScenario = scenario("commandButton scenario")
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
    commandButtonScenario.inject(atOnceUsers(2))
  )
    .protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.greaterThan(95)
    )


}