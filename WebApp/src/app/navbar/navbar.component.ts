import {Component, OnInit} from '@angular/core';
import {LoginServiceService} from "../login-service.service";

@Component({
  selector:'app-navbar', templateUrl:'./navbar.component.html', styleUrls:['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  constructor(private ls: LoginServiceService) {
  }

  ngOnInit() {
  }

  logOut() {
    this.ls.setLogin("");
  }

}
