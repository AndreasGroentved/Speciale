import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {WebService} from '../web.service';
import {Procuration} from '../Procuration';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';


@Component({
  selector: 'app-procuration',
  templateUrl: './procuration.component.html',
  styleUrls: ['./procuration.component.css']
})
export class ProcurationComponent implements OnInit {

  constructor(private route: ActivatedRoute, private router: Router, private webService: WebService) {
  }

  status: string;

  recievedAccepted: Array<Procuration>;
  recievedExpired: Array<Procuration>;
  recievedPending: Array<Procuration>;

  recievedAcceptedCollapsed = true;
  recievedExpiredCollapsed = true;
  recievedPendingCollapsed = true;

  sentAccepted: Array<Procuration>;
  sentExpired: Array<Procuration>;
  sentPending: Array<Procuration>;

  sentAcceptedCollapsed = true;
  sentExpiredCollapsed = true;
  sentPendingCollapsed = true;


  ngOnInit() {
    this.status = this.route.snapshot.paramMap.get('status');
    this.webService.getReceivedPendingProcurations(val => this.recievedPending = val);
    this.webService.getReceivedAcceptedProcurations(val => this.recievedAccepted = val);
    this.webService.getReceivedExpiredProcurations(val => this.recievedExpired = val);
    this.webService.getSentPendingProcurations(val => this.sentPending = val);
    this.webService.getSentAcceptedProcurations(val => this.sentAccepted = val);
    this.webService.getSentExpiredProcurations(val => this.sentExpired = val);
  }

  acceptProc(messageChainID: string) {
    this.webService.acceptProcuration(messageChainID);
    this.webService.getReceivedAcceptedProcurations(val => this.recievedAccepted = val);
    this.webService.getReceivedPendingProcurations(val => this.recievedPending = val);
  }

  rejectProc(messageChainID: string) {
    this.webService.rejectProcuration(messageChainID);
    this.webService.getReceivedPendingProcurations(val => this.recievedPending = val);
    this.webService.getReceivedAcceptedProcurations(val => this.recievedAccepted = val);
  }
}
