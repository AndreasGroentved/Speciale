import {Component, OnInit} from '@angular/core';
import {WebService} from "../web.service";
import {MatSnackBar} from "@angular/material";
import {WebSocketService} from "../chat-service.service";

@Component({
  selector: 'app-house-overview',
  templateUrl: './house-overview.component.html',
  styleUrls: ['./house-overview.component.css']
})

export class HouseOverviewComponent implements OnInit {

  constructor(private  ws: WebService, private snackBar: MatSnackBar, private chat: WebSocketService) {
  }

  ngOnInit(): void {
  }
}
