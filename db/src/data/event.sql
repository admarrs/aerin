create table event (
	id    serial primary key,
  observed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
	title text not null,
  location geometry,
  metadata jsonb,
  category text not null,
	created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
	owner_id int references "user"(id) default request.user_id()
);

create trigger set_timestamp
  before update on event
  for each row
execute procedure trigger_set_timestamp();

-- attach the trigger to send events to rabbitmq
-- there is a 8000 bytes hard limit on the message payload size (PG NOTIFY) so it's better not to send data that is not used
-- on_row_change call can take the following forms
-- on_row_change() - send all columns
-- on_row_change('{"include":["id"]}'::json) - send only the listed columns
-- on_row_change('{"exclude":["bigcolumn"]}'::json) - exclude listed columns from the payload

--create trigger send_todo_change_event
--after insert or update or delete on todo
--for each row execute procedure rabbitmq.on_row_change('{"include":["id","todo"]}');