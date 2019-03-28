import {Component, OnInit} from '@angular/core';
import {WebService} from "../web.service";
import {ActivatedRoute, Router} from "@angular/router";
import {Location} from '@angular/common';
import {TangleDeviceSpecification} from "../TangleDeviceSpecification";

@Component({
  selector: 'app-request-device',
  templateUrl: './request-device.component.html',
  styleUrls: ['./request-device.component.css']
})
export class RequestDeviceComponent implements OnInit {

  private id: string;
  fromDate = "1988-10-10T10:00";
  toDate = "1988-10-10T10:00";
  tangleDevice: TangleDeviceSpecification;

  constructor(private route: ActivatedRoute, private router: Router, private ws: WebService, private location: Location) {
  }


  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id');
    const navigation = this.router.getCurrentNavigation();
    this.tangleDevice = navigation.extras.state.device;
  }

  requestDeviceAccess() {
    console.log(this.fromDate);
    let fromD = new Date(this.fromDate);
    let toD = new Date(this.toDate);
    this.ws.requestDevice(this.tangleDevice.publicKey, this.id, fromD, toD, val => {
      if (!val.hasOwnProperty("error")) this.location.back();
      else alert("yo request failed");
    });
  }
}
