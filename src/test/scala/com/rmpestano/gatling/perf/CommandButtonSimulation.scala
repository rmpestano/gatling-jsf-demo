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

  val growlIdCheck = css("span[id$='growl']", "id")
    .saveAs("growlId")

  val formButtonCheck = css("div[class$='Implementation'] > form","id") //first form is for themeSwitcher, see html source   
   .saveAs("buttonForm")


  val growlResponseCheck = xpath("//*[contains(text(),'Welcome to Primefaces!!')]")

  def ajaxButtonCall = jsfPartialPost("request_ajax_button", "/ui/button/commandButton.xhtml")
    .formParam("javax.faces.source", "${btAjax}")
    .formParam("javax.faces.partial.execute", "@all")
    .formParam("javax.faces.partial.render", "${growlId}")
    .formParam("${btAjax}", "${btAjax}")
    .formParam("${buttonForm}", "${buttonForm}")
    .check(status.is(200), growlResponseCheck)

  def normalButtonCall =
    jsfPost("normal_request", "/ui/button/commandButton.xhtml")
      .formParam("${btNonAjax}", "${btNonAjax}")
      .formParam("${buttonForm}", "${buttonForm}")
      .check(status.is(200))

  val commandButtonScenario = scenario("commandButton scenario")
    .exec(
      jsfGet("saveState", "/ui/button/commandButton.xhtml")
        .check(status.is(200), ajaxButtonIdCheck, formButtonCheck, nonAjaxButtonIdCheck, growlIdCheck)
    )
    .pause(2)
    .exec(ajaxButtonCall)
    .pause(100 milliseconds)
    .exec(normalButtonCall)
    .pause(1)

  //just prints the session
  printSession

  setUp(
    commandButtonScenario.inject(atOnceUsers(1))
  )
    .protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.greaterThan(95)
    )


}