import {Component, Input, OnInit} from '@angular/core';
import * as CanvasJS from "../../assets/canvasjs.min.js";
import {WebService} from "../web.service";
import {ClientResponse} from "../ClientResponse";
import {TimeUsage} from "../TimeUsage";

@Component({
  selector: 'app-power',
  templateUrl: './power.component.html',
  styleUrls: ['./power.component.css']
})
export class PowerComponent implements OnInit {
  @Input() deviceId: string;

  constructor(private ws: WebService) {
  }

  ngOnInit() {
    this.ws.getOnTime(this.deviceId, map => {
      console.log(map);
      console.log((map as ClientResponse).result);
      this.setChart((map as ClientResponse).result);
    })
  }


  setChart(valueMap: Array<TimeUsage>) {
    let dataPoints = [];
    valueMap.forEach(val => {
      let y = (val.usage / 1000) / 60;
      dataPoints.push({x: val.time, y: y});
    });

    let chart = new CanvasJS.Chart("chartContainer", {
      zoomEnabled: true,
      animationEnabled: true,
      exportEnabled: true,
      title: {
        text: "On time for device"
      },
      subtitles: [{
        text: "Try Zooming and Panning"
      }],
      data: [{
        type: "line",
        dataPoints: dataPoints
      }],
      axisX: {
        labelFormatter: function (e) {
          let date = new Date(e.value);
          let min = date.getMinutes();
          let out = min.toString();
          if (min < 10) out = "0" + out;
          return date.getHours() + ":" + out;
        }
      },
    }); 

    chart.render();
  }


}
