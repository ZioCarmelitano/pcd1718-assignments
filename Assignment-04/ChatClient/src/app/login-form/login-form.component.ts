import {Component, OnInit} from '@angular/core';
import {ChatUser} from "../chat-user";
import {ChatService} from "../chat.service";

@Component({
  selector: 'app-login-form',
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.css']
})
export class LoginFormComponent implements OnInit {

  user: ChatUser;

  constructor(private service: ChatService) {
  }

  ngOnInit() {
    this.user = new ChatUser("...");
  }

  login() {
    ChatService.user = this.user;
  }
}