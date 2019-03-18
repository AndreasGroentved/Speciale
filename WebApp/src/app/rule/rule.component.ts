import {Component, OnInit} from '@angular/core';
import {WebService} from "../web.service";

@Component({
  selector: 'app-rule',
  templateUrl: './rule.component.html',
  styleUrls: ['./rule.component.css']
})
export class RuleComponent implements OnInit {

  inputText = "";

  constructor(private ws: WebService) {
  }

  ngOnInit() {
    this.ws.getRules(val => {
      this.inputText = val.result;
    });
  }

  update() {
    this.ws.updateRules(this.inputText, val => console.log("yolo"));
  }
}
