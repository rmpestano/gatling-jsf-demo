package com.rmpestano.gatling.perf

import _root_.io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.core.session.Expression
import com.rmpestano.gatling.perf.Commons._

class DatatableSelectionSimulation extends Simulation {

  val buttonIdCheck = css("button[class*='ui-button-icon-only']")
    .saveAs("buttonId")

  //if car dialog is present, the call was successful
  // dialog partial response contains <div id="form:carDetail"
  val dialogFormCheck = xpath("//*[@id='form:carDetail']")



  def datatableSelectCar = jsfPartialPost("request_datatable_select_car", "/ui/data/datatable/selection.xhtml")
    .formParam("javax.faces.source", "${buttonId}")
    .formParam("javax.faces.partial.execute", "@all")
    .formParam("javax.faces.partial.render", "form:carDetail")
    .formParam("form", "${form}")
    .check(status.is(200), dialogFormCheck)

  val DatatableSelectScenario = scenario("datatable selection scenario")
    .exec(
      jsfGet("initialRequest", "/ui/data/datatable/selection.xhtml")
        .check(status.is(200), buttonIdCheck, formIdCheck)
    )
    .pause(2)
    .exec(datatableSelectCar)
    .pause(1)

  setUp(
    DatatableSelectScenario.inject(atOnceUsers(2))
  )
    .protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.greaterThan(99)
    )

}

