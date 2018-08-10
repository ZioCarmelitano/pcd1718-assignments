import {Component, OnInit} from '@angular/core';

import {Message} from '../message';
import {ChatService} from "../chat.service";
import {User} from "../user";

@Component({
  selector: 'app-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.css']
})
export class MessagesComponent implements OnInit {

  public user: User;

  public messages: Message[] = [];

  constructor(private service: ChatService) {
    this.user = this.service.user;
  }

  ngOnInit() {
    this.service.onNewMessage().subscribe(msg => this.messages.push(msg));
  }
}
