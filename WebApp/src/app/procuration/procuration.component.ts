import{Component, OnInit}from '@angular/core';
import {ActivatedRoute, Router}from '@angular/router';
import {WebService} from '../web.service';
import {DeviceSpecification}from '../DeviceSpecification';
import {DeviceResource}from '../DeviceResource';
import {Procuration}from '../Procuration';


@Component({
selector: 'app-procuration',
templateUrl: './procuration.component.html',
styleUrls: ['./procuration.component.css']
})
export class ProcurationComponent implements OnInit {

constructor(private route: ActivatedRoute, private router: Router, private webService: WebService) {
  }

  private status: string;

  private accepted: Array<Procuration>;
  private expired: Array<Procuration>;
  private pending: Array<Procuration>;


  ngOnInit() {
    this.status = this.route.snapshot.paramMap.get('status');
    this.webService.getPendingProcurations(val => this.pending = val);
    this.webService.getAcceptedProcurations(val => this.accepted = val);
    this.webService.getExpiredProcurations(val => this.expired = val);
    console.log(this.accepted);
  }

  acceptProc(messageChainID: string) {
    this.webService.acceptProcuration(messageChainID)
    this.webService.getAcceptedProcurations(val => this.accepted = val);
    this.webService.getPendingProcurations(val => this.pending = val);
  }
  rejectProc(messageChainID: string) {
    this.webService.rejectProcuration(messageChainID)
    this.webService.getPendingProcurations(val => this.pending = val);
    this.webService.getAcceptedProcurations(val => this.accepted = val);
  }

}
