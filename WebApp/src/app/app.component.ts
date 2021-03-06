import {Component} from '@angular/core';
import {MatSnackBar} from "@angular/material";
import {WebSocketService} from "./chat-service.service";
import {WebService} from "./web.service";

@Component({
  selector:'app-root', templateUrl:'./app.component.html', styleUrls:['./app.component.css']
})
export class AppComponent {
  title = 'WebApp';

  constructor(private snackBar: MatSnackBar, private webSocketService: WebSocketService, private ws: WebService) {

  }

  openSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, {
      duration:2000,
    });
  }

  ngOnInit() {
    this.webSocketService.connect("ws://localhost:4567/messageChannel").subscribe(value => {
      this.openSnackBar(value.data, 'act');
      console.log(value);
    });

  }
}
