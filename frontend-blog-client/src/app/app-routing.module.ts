import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { PageNotFoundComponent } from './components/page-not-found/page-not-found.component';
import { PostsComponent } from './components/posts/posts.component';
import { NewPostComponent } from './components/newpost/newpost.component';

const routes: Routes = [
  { path: 'home', component: HomeComponent, data: { route_id: 'Home'}},
  { path: 'read', component: PostsComponent, data: { route_id: 'Blog'}},
  { path: 'blog',   redirectTo: '/read'},
  { path: 'post', component: NewPostComponent, data: { route_id: 'NewPost'}},
  { path: 'new',   redirectTo: '/post'},
  { path: '',   redirectTo: '/home', pathMatch: 'full'},
  { path: '**', component: PageNotFoundComponent , data: { route_id: '404'}}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
