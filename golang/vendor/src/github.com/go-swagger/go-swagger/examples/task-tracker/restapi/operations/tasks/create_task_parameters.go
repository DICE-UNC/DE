package tasks

// This file was generated by the swagger tool.
// Editing this file might prove futile when you re-run the swagger generate command

import (
	"io"
	"net/http"

	"github.com/go-swagger/go-swagger/errors"
	"github.com/go-swagger/go-swagger/httpkit"
	"github.com/go-swagger/go-swagger/httpkit/middleware"

	"github.com/go-swagger/go-swagger/examples/task-tracker/models"
)

// NewCreateTaskParams creates a new CreateTaskParams object
// with the default values initialized.
func NewCreateTaskParams() CreateTaskParams {
	var ()
	return CreateTaskParams{}
}

// CreateTaskParams contains all the bound params for the create task operation
// typically these are obtained from a http.Request
//
// swagger:parameters createTask
type CreateTaskParams struct {

	// HTTP Request Object
	HTTPRequest *http.Request

	/*The task to create
	  Required: true
	  In: body
	*/
	Body *models.Task
}

// BindRequest both binds and validates a request, it assumes that complex things implement a Validatable(strfmt.Registry) error interface
// for simple values it will use straight method calls
func (o *CreateTaskParams) BindRequest(r *http.Request, route *middleware.MatchedRoute) error {
	var res []error
	o.HTTPRequest = r

	if httpkit.HasBody(r) {
		defer r.Body.Close()
		var body models.Task
		if err := route.Consumer.Consume(r.Body, &body); err != nil {
			if err == io.EOF {
				res = append(res, errors.Required("body", "body"))
			} else {
				res = append(res, errors.NewParseError("body", "body", "", err))
			}

		} else {
			if err := body.Validate(route.Formats); err != nil {
				res = append(res, err)
			}

			if len(res) == 0 {
				o.Body = &body
			}
		}

	} else {
		res = append(res, errors.Required("body", "body"))
	}

	if len(res) > 0 {
		return errors.CompositeValidationError(res...)
	}
	return nil
}
