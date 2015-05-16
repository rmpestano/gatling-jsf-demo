package com.rmpestano.gatling.perf

import _root_.io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.core.session.Expression
import com.rmpestano.gatling.perf.Commons._

class AjaxEventSimulation extends Simulation {

  val inputIdCheck = css("input[id$='firstname']", "id")
    .saveAs("inputId") //stores in gatling session id of input with id that endswith 'firstname'

  val outputIdCheck = css("span[id$='out1']", "id")
    .saveAs("outputId")

  //checks the partial response xml result, example:
  //<partial-response id="j_id1"><changes><update id="j_idt87:out1"><![CDATA[<span id="j_idt87:out1">Gatling, JSF and Primefaces rules</span>]]>
  val outputValueCheck = xpath("//*[contains(@id,'out1') and contains(text(),'Gatling, JSF and Primefaces rules')]")


  def ajaxEventRequest = jsfPartialPost("request_ajax_event", "/ui/ajax/event.xhtml")
    .formParam("javax.faces.source", "${inputId}")
    .formParam("javax.faces.partial.execute", "${inputId}")
    .formParam("javax.faces.behavior.event", "keyup")
    .formParam("javax.faces.partial.event", "keyup")
    .formParam("javax.faces.partial.render", "${outputId}")
    .formParam("${inputId}", "Gatling, JSF and Primefaces rules")
    .formParam("${form}", "${form}")
    .check(status.is(200), outputValueCheck)

  val AjaxEventScenario = scenario("ajaxEvent scenario")
    .exec(
      jsfGet("initialRequest", "/ui/ajax/event.xhtml")
        .check(status.is(200), inputIdCheck, outputIdCheck, formIdCheck)
    )
    .pause(2)
    .exec(ajaxEventRequest)
    .pause(1)

  setUp(
    AjaxEventScenario.inject(atOnceUsers(2))
  )
    .protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.greaterThan(99)
    )

}
