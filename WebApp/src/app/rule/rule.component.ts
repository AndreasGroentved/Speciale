import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {WebService} from '../web.service';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-rule',
  templateUrl: './rule.component.html',
  styleUrls: ['./rule.component.css']
})
export class RuleComponent implements OnInit {
  inputText = '';
  collapsed = true;
  errorText = '';

  constructor(private ws: WebService) {
  }

  ngOnInit() {
    this.ws.getRules(val => {
      this.inputText = val;
    });
  }

  update() {
    this.ws.updateRules(this.inputText, val => {
      this.errorText = val;
    });
  }
}
