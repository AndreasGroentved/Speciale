import {Component, OnInit} from '@angular/core';
import {WebService} from "../web.service";
import {Router} from "@angular/router";
import {LoginServiceService} from "../login-service.service";

@Component({
  selector:'app-login', templateUrl:'./login.component.html', styleUrls:['./login.component.css']
})
export class LoginComponent implements OnInit {
  username: string;
  password: string;
  errorMessage: string = "";

  constructor(private webservice: WebService, private router: Router, private ls: LoginServiceService) {
  }

  ngOnInit() {
    console.log(this.ls.token);
    if (this.ls.token != "") this.router.navigate(['/house_overview']);
  }

  login() {
    this.webservice.login(this.username, this.password, (token) => {
      if (token != "") {
        console.log(token);
        this.ls.setLogin(token);
        this.router.navigate(['/house_overview']);
      } else this.errorMessage = "invalid login"
    })
  }

}
