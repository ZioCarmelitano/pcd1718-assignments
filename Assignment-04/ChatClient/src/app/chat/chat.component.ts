import {Component, OnInit} from '@angular/core';
import {User} from "../user";
import {ChatUser} from "../chat-user";

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit {

  public user: ChatUser;

  constructor() {}

  ngOnInit() {
  }

}
