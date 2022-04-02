drop schema if exists data cascade;
create schema data;
set search_path = data, public;

create or replace function trigger_set_timestamp()
  returns trigger as $$
begin
  new.updated_at = now();
  return new;
end;
$$ language plpgsql;

-- import our application models
\ir user.sql
\ir event.sql