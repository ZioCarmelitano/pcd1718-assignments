import {Component, Input, OnInit} from '@angular/core';

import {Room} from '../room';
import {ChatUser} from "../chat-user";
import {User} from "../user";
import {ChatService} from "../chat.service";

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {

  @Input()
  public user: User;

  public rooms: Room[];

  constructor() {
  }

  ngOnInit() {
    this.rooms = [
      {
        id: 1,
        name: 'Aula A'
      },
      {
        id: 2,
        name: 'Aula B'
      },
      {
        id: 3,
        name: 'Aula C'
      }
    ];
  }

}
