import {Component, OnInit} from '@angular/core';
import {ChatService} from "../chat.service";
import {User} from "../user";

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit {

  public user: User;

  constructor() {
    this.user = ChatService.user;
    console.log("nome utente: " + this.user.name);
  }

  ngOnInit() {
  }

}
