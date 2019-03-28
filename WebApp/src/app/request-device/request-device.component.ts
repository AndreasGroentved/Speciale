import {Component, OnInit} from '@angular/core';
import {WebService} from "../web.service";
import {ActivatedRoute, Router} from "@angular/router";
import {Location} from '@angular/common';
import {TangleDeviceSpecification} from "../TangleDeviceSpecification";
import {DeviceDataService} from "../device-data.service";

@Component({
  selector: 'app-request-device',
  templateUrl: './request-device.component.html',
  styleUrls: ['./request-device.component.css']
})
export class RequestDeviceComponent implements OnInit {

  private id: string;
  fromDate = "2019-10-10T10:00";
  toDate = "2019-10-17T10:00";
  tangleDevice: TangleDeviceSpecification;
  addressTo: string;

  constructor(private route: ActivatedRoute, private router: Router, private ws: WebService, private location: Location, private deviceService: DeviceDataService) {
  }


  ngOnInit() {

    this.id = this.route.snapshot.paramMap.get('id');
    /*console.log(this.route.snapshot.paramMap);
    console.log(this.route.snapshot)
    this.route.queryParamMap.subscribe(params => {
      console.log(params);
    });*/
    /*const navigation = this.router.getCurrentNavigation();
    console.log(navigation);*/
    //console.log(navigation.extras.state);
    /* const navigation = this.router.getCurrentNavigation();
    console.log(navigation.extras);
    this.tangleDevice = navigation.extras.state.device;*/
    this.tangleDevice = this.deviceService.deviceSpecification;
    this.addressTo = this.deviceService.addressTo;
  }

  requestDeviceAccess() {
    console.log(this.fromDate);
    let fromD = new Date(this.fromDate);
    let toD = new Date(this.toDate);
    this.ws.requestDevice(this.addressTo, this.id, fromD, toD, this.tangleDevice, val => {
      if (!val.hasOwnProperty("error")) this.location.back();
      else alert("yo request failed");
    });
  }
}
