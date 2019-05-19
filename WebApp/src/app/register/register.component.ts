import {Component, OnInit} from '@angular/core';
import {WebService} from "../web.service";

@Component({
  selector:'app-register', templateUrl:'./register.component.html', styleUrls:['./register.component.css']
})
export class RegisterComponent implements OnInit {
  username: string = '';
  password: string = '';

  constructor(private ws: WebService) {
  }

  ngOnInit() {
  }

  register() {
    this.ws.register(this.username, this.password, (a) => {
      alert(a);
    })
  }

}
