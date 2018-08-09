import {Component, Input, OnInit} from '@angular/core';

import {Room} from '../room';
import {User} from '../user';

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
    this.user =
      {
        id: 0,
        name: 'marco'
      };
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
