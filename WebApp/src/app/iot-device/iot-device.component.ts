import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {WebService} from "../web.service";

@Component({
  selector: 'app-iot-device',
  templateUrl: './iot-device.component.html',
  styleUrls: ['./iot-device.component.css']
})
export class IotDeviceComponent implements OnInit {

  constructor(private route: ActivatedRoute, private router: Router, private webService: WebService) {
  }

  private id: string;

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id');
    console.log(this.id);
    this.webService.getDevice(this.id, device => {

    })
  }

}
