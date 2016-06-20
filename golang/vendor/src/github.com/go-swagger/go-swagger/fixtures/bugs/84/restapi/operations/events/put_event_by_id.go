package events

// This file was generated by the swagger tool.
// Editing this file might prove futile when you re-run the generate command

import (
	"net/http"

	"github.com/go-swagger/go-swagger/httpkit/middleware"
)

// PutEventByIDHandlerFunc turns a function with the right signature into a put event by id handler
type PutEventByIDHandlerFunc func(PutEventByIDParams) middleware.Responder

// Handle executing the request and returning a response
func (fn PutEventByIDHandlerFunc) Handle(params PutEventByIDParams) middleware.Responder {
	return fn(params)
}

// PutEventByIDHandler interface for that can handle valid put event by id params
type PutEventByIDHandler interface {
	Handle(PutEventByIDParams) middleware.Responder
}

// NewPutEventByID creates a new http.Handler for the put event by id operation
func NewPutEventByID(ctx *middleware.Context, handler PutEventByIDHandler) *PutEventByID {
	return &PutEventByID{Context: ctx, Handler: handler}
}

/*PutEventByID swagger:route PUT /events/{id} events putEventById

Update existing event.

*/
type PutEventByID struct {
	Context *middleware.Context
	Params  PutEventByIDParams
	Handler PutEventByIDHandler
}

func (o *PutEventByID) ServeHTTP(rw http.ResponseWriter, r *http.Request) {
	route, _ := o.Context.RouteInfo(r)
	o.Params = NewPutEventByIDParams()

	if err := o.Context.BindValidRequest(r, route, &o.Params); err != nil { // bind params
		o.Context.Respond(rw, r, route.Produces, route, err)
		return
	}

	res := o.Handler.Handle(o.Params) // actually handle the request

	o.Context.Respond(rw, r, route.Produces, route, res)

}
