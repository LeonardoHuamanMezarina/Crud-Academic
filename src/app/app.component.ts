import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: '<router-outlet/>'
  //templateUrl: './app.component.html',
  //styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'my-project';
}
