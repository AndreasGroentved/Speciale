import {Component, Input, OnInit} from '@angular/core';
import {DeviceResource} from "../DeviceResource";
import * as CanvasJS from "../../assets/canvasjs.min.js";

@Component({
  selector: 'app-power',
  templateUrl: './power.component.html',
  styleUrls: ['./power.component.css']
})
export class PowerComponent implements OnInit {
  @Input() deviceId: string;

  constructor() {
  }

  ngOnInit() {
    let dataPoints = [];
    let y = 0;
    for ( var i = 0; i < 100; i++ ) {
      y = Math.round(5 + Math.random() * (-5 - 5));
      dataPoints.push({x:i, y: y});
    }
    let chart = new CanvasJS.Chart("chartContainer", {
      zoomEnabled: true,
      animationEnabled: true,
      exportEnabled: true,
      title: {
        text: "Performance Demo - 10000 DataPoints"
      },
      subtitles:[{
        text: "Try Zooming and Panning"
      }],
      data: [
        {
          type: "line",
          dataPoints: dataPoints
        }]
    });

    chart.render();
  }


}
