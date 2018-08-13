import { Component, OnInit } from '@angular/core';

import { Message } from '../message';
import { ChatService } from '../chat.service';
import { Room } from '../room';
import { User } from '../user';

@Component({
  selector: 'app-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.css']
})
export class MessagesComponent implements OnInit {

  public user: User;
  public room: Room;

  public messages: any[] = [];

  constructor(private service: ChatService) {
    this.user = this.service.user;
    this.room = this.service.room;
  }

  ngOnInit() {
    this.service.onNewMessage().subscribe(message => {
      console.log(message);
      if (message.error) {
        if (message.user === this.user) {
          console.error(message.error);
        }
      }
      this.messages.push(message);
    });
    this.service.onJoinRoom().subscribe(data => {
      if (data.user.id === this.user.id) {
        this.messages = [];
      } else if (data.roomId === this.room.id) {
        this.messages.push(data.user.name + ' joined the room');
      }
    });
    this.service.onLeaveRoom().subscribe(data => {
      if (data.roomId === this.room.id) {
        this.messages.push(data.user.name + ' left the room');
      }
    });
    this.service.onEnterCS().subscribe(data => {
      if (data.roomId === this.room.id && data.user.id !== this.user.id) {
        this.messages.push(data.user.name + ' acquired the critical section');
      }
    });
    this.service.onExitCS().subscribe(data => {
      if (data.roomId === this.room.id && data.user.id !== this.user.id) {
        this.messages.push(data.user.name + ' released the critical section');
      }
    });
    this.service.onTimeoutExpired().subscribe(room => {
      if (room.id === this.room.id) {
        this.messages.push('Critical section timeout expired');
      }
    });
  }

}
