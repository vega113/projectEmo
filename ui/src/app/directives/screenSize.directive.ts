import { Directive, ElementRef, Input, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';

@Directive({
  selector: '[appScreenSize]'
})
export class ScreenSizeDirective implements OnInit, OnDestroy {
  @Input() largeScreenMode = 'side';
  @Input() smallScreenMode = 'over';

  private screenSizeSubscription!: Subscription;

  constructor(private el: ElementRef, private breakpointObserver: BreakpointObserver) {}

  ngOnInit() {
    this.screenSizeSubscription = this.breakpointObserver.observe([Breakpoints.Small, Breakpoints.HandsetPortrait]).subscribe(result => {
      if (result.matches) {
        this.el.nativeElement.mode = this.smallScreenMode;
      } else {
        this.el.nativeElement.mode = this.largeScreenMode;
      }
    });
  }

  ngOnDestroy() {
    if (this.screenSizeSubscription) {
      this.screenSizeSubscription.unsubscribe();
    }
  }
}
