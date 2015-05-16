package com.rmpestano.gatling.perf

import _root_.io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.core.session.Expression
import com.rmpestano.gatling.perf.Commons._

class DatatableSelectionSimulation extends Simulation {

  val buttonIdCheck = css("button[class*='ui-button-icon-only']","id")
    .saveAs("buttonId")

  //if car dialog is present, the call was successful
  // dialog partial response contains: <partial-response id="j_id1"><changes><update id="form:carDetail"><![CDATA[<div id="form:carDetail" class="ui-outputpanel ui-widget" style="text-align:center;"><table id="form:j_idt152"
  val dialogFormCheck = css("img[src*='/showcase/javax.faces.resource/demo/images/car/']","src")
    .saveAs("dialogImg")



  def datatableSelectCar = jsfPartialPost("request_datatable_select_car", "/ui/data/datatable/selection.xhtml")
    .formParam("javax.faces.source", "${buttonId}")
    .formParam("javax.faces.partial.execute", "@all")
    .formParam("javax.faces.partial.render", "form:carDetail")
    .formParam("form", "form")
    .formParam("${buttonId}","${buttonId}")
    .check(status.is(200), dialogFormCheck)

  val DatatableSelectScenario = scenario("datatable selection scenario")
    .exec(
      jsfGet("initialRequest", "/ui/data/datatable/selection.xhtml")
        .check(status.is(200), buttonIdCheck, formIdCheck)
    )
    .pause(2)
    .exec(datatableSelectCar)
    .pause(1)
    .exec(session => {
    println(session)
    session
  })

  setUp(
    DatatableSelectScenario.inject(atOnceUsers(2))
  )
    .protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.greaterThan(99)
    )

}

