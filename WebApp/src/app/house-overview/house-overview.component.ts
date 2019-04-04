import {Component, OnInit} from '@angular/core';
import {WebService} from "../web.service";
import {MatSnackBar} from "@angular/material";
import {ChatService} from "../chat-service.service";

@Component({
  selector: 'app-house-overview',
  templateUrl: './house-overview.component.html',
  styleUrls: ['./house-overview.component.css']
})

export class HouseOverviewComponent implements OnInit {

  constructor(private  ws: WebService, private snackBar: MatSnackBar, private chat: ChatService) {
  }

  openSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, {
      duration: 2000,
    });
  }

  ngOnInit() {
    this.chat.connect("ws://localhost:4567/messageChannel");
    /*  this.ws.onMessage().subscribe(value => {
        console.log(value);
        this.openSnackBar(value.text, 'act');
      });*/
  }

}
