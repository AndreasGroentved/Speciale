import {Component, Input, OnInit} from '@angular/core';
import {DeviceResource} from "../DeviceResource";

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
  }

}
