import {Component, OnInit} from '@angular/core';
import {User} from "../user";

@Component({
  selector: 'app-index',
  templateUrl: './index.component.html',
  styleUrls: ['./index.component.css']
})
export class IndexComponent implements OnInit {

  public user: User;

  constructor() {
  }

  ngOnInit() {
    this.user = {
      id: 1,
      name: 'Pippo'
    };
  }

}
