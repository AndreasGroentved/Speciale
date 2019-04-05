import {Component} from '@angular/core';
import {MatSnackBar} from "@angular/material";
import {ChatService} from "./chat-service.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'WebApp';

  constructor(private snackBar: MatSnackBar, private chat: ChatService) {
    
  }

  openSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, {
      duration: 2000,
    });
  }

  ngOnInit() {
    this.chat.connect("ws://localhost:4567/messageChannel").subscribe(value => {
      this.openSnackBar(value.data, 'act');
      console.log(value);
    });
  }
}
