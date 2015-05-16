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

  val tableRowCheck = css("tbody[id='form:eventsDT_data'] > tr[role='row'] > td[role='gridcell']")
   .saveAs("rowId")

  //datatable rowSelect response: <partial-response id="j_id1"><changes><update id="form:msgs"><![CDATA[<span id="form:msgs" class="ui-growl-pl" data-widget="widget_form_msgs" data-summary="data-summary" data-detail="data-detail" data-severity="all,error" data-redisplay="true"></span><script id="form:msgs_s" type="text/javascript">$(function(){PrimeFaces.cw('Growl','widget_form_msgs',{id:'form:msgs',sticky:false,life:6000,escape:true,msgs:[{summary:"Car Selected",detail:"c022689b",severity:'info'}]});});</script>]]></update><update id="j_id1:javax.faces.ViewState:0"><![CDATA[6671433459483031284:-6868499056774026453]]></update></changes></partial-response>
  val growlCheck = css("script[id$='msgs_s']")
  .saveAs("growlValue") //just to confirm in printSession



  def datatableSelectCarWithButton = jsfPartialPost("request_datatable_select_car", "/ui/data/datatable/selection.xhtml")
    .formParam("javax.faces.source", "${buttonId}")
    .formParam("javax.faces.partial.execute", "@all")
    .formParam("javax.faces.partial.render", "form:carDetail")
    .formParam("form", "form")
    .formParam("${buttonId}","${buttonId}")
    .check(status.is(200), dialogFormCheck)

  def datatableSelectCarRowEvent = jsfPartialPost("request_datatable_select_car", "/ui/data/datatable/selection.xhtml")
    .formParam("javax.faces.source", "form:eventsDT")
    .formParam("javax.faces.partial.execute", "form:eventsDT")
    .formParam("javax.faces.partial.render", "form:msgs")
    .formParam("form", "form")
    .formParam("form:eventsDT_instantSelectedRowKey","${rowId}")
    .formParam("javax.faces.behavior.event","rowSelect")
    .formParam("javax.faces.partial.event","rowSelect")
    .check(status.is(200), growlCheck)

  val DatatableSelectScenario = scenario("datatable selection scenario")
    .exec(
      jsfGet("initialRequest", "/ui/data/datatable/selection.xhtml")
        .check(status.is(200), buttonIdCheck, tableRowCheck)
    )
    .pause(1)
    .exec(datatableSelectCarWithButton)
    .pause(300 milliseconds)
    .exec(datatableSelectCarRowEvent)
    .pause(300 milliseconds)
    printSession

  setUp(
    DatatableSelectScenario.inject(atOnceUsers(1))
  )
    .protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.greaterThan(99)
    )

}

