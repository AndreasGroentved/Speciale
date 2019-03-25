import {Component, OnInit} from '@angular/core';
import {WebService} from "../web.service";

@Component({
  selector: 'app-tangle-devices',
  templateUrl: './tangle-devices.component.html',
  styleUrls: ['./tangle-devices.component.css']
})
export class TangleDevicesComponent implements OnInit {



  constructor(private webService: WebService) {
  }

  ngOnInit() {

  }

}
