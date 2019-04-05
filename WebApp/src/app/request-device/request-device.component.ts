import {Component, OnInit} from '@angular/core';
import {WebService} from '../web.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Location} from '@angular/common';
import {TangleDeviceSpecification} from '../TangleDeviceSpecification';
import {DeviceDataService} from '../device-data.service';
import DateTimeFormat = Intl.DateTimeFormat;
import {DateFormatter} from '@angular/common/src/pipes/deprecated/intl';
import {toDate} from '@angular/common/src/i18n/format_date';

@Component({
  selector: 'app-request-device',
  templateUrl: './request-device.component.html',
  styleUrls: ['./request-device.component.css']
})
export class RequestDeviceComponent implements OnInit {

  id: string;
  dateFormatter: DateTimeFormat;
  fromDate = '';
  toDate = '';
  tangleDevice: TangleDeviceSpecification;
  addressTo: string;

  constructor(private route: ActivatedRoute, private router: Router, private ws: WebService, private location: Location,
              private deviceService: DeviceDataService) {
  }


  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id');
    this.tangleDevice = this.deviceService.deviceSpecification;
    this.addressTo = this.deviceService.addressTo;
    const date = new Date().toISOString();
    this.fromDate = date.substring(0, date.length - 1);
    this.toDate = date.substring(0, date.length - 1);
  }

  requestDeviceAccess() {
    const fromD = new Date(this.fromDate);
    const toD = new Date(this.toDate);
    this.ws.requestDevice(this.addressTo, this.id, fromD, toD, this.tangleDevice, val => {
      if (!val.hasOwnProperty('error')) {
        this.location.back();
      } else {
        alert('yo request failed');
      }
    });
  }
}
