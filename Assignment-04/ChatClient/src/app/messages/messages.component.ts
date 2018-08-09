import {Component, Input, OnInit} from '@angular/core';

import {Room} from '../room';
import {User} from '../user';
import {Message} from '../message';

@Component({
  selector: 'app-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.css']
})
export class MessagesComponent implements OnInit {

  @Input()
  public user: User;
  @Input()
  public room: Room;

  public messages: Message[];

  constructor() {
  }

  ngOnInit() {
    this.getMessages(this.room);
    this.messages = [
      {
        room: this.room,
        user: this.user,
        message: "How the hell am I supposed to get a jury to believe you when I am not even sure that I do"
      },
      {
        room: this.room,
        user: this.user,
        message: "How the hell am I supposed to get a jury to believe you when I am not even sure that I do"
      },
      {
        room: this.room,
        user: this.user,
        message: "How the hell am I supposed to get a jury to believe you when I am not even sure that I do"
      },
      {
        room: this.room,
        user: this.user,
        message: "How the hell am I supposed to get a jury to believe you when I am not even sure that I do"
      },
      {
        room: this.room,
        user: this.user,
        message: "How the hell am I supposed to get a jury to believe you when I am not even sure that I do"
      },
      {
        room: this.room,
        user: this.user,
       message: "How the hell am I supposed to get a jury to believe you when I am not even sure that I do"
      }
    ]
  }

  private getMessages(params:Room) {

  }

}
